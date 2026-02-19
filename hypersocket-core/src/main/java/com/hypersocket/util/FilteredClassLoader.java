package com.hypersocket.util;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 * A "filtering parent" ClassLoader that can pretend certain packages/classes/resources do not exist,
 * even if they exist in (or were already loaded by) its parent (including the system class loader).
 *
 * Typical use:
 *   ClassLoader parent = ClassLoader.getSystemClassLoader();
 *   ClassLoader hidingParent = HidingClassLoader.builder(parent)
 *       .hidePackage("com.acme.internal")   // hides com.acme.internal and all subpackages
 *       .hideClass("org.example.Secret")    // hides only that class
 *       .hideModuleInfo(true)               // optional: hide module-info.class resources
 *       .build();
 *
 *   // Child uses hidingParent as its parent; if child uses parent-first, it will call parent.loadClass(),
 *   // which is this loader, and hidden classes will appear "not found".
 *   Class<?> c = child.loadClass("com.acme.internal.Foo"); // -> ClassNotFoundException
 *
 * Notes about JPMS modules:
 *   - ClassLoaders don't "load modules" in the JPMS sense; ModuleLayer does resolution.
 *   - What we *can* do reliably is hide the *classes/resources* belonging to a module by hiding their packages.
 *   - Optionally, you can also hide "module-info.class" resources to make module descriptors harder to discover.
 */
public final class FilteredClassLoader extends ClassLoader {

    // Package prefixes like "com.foo" meaning "com.foo.*"
    private final Set<String> hiddenPackagePrefixes;
    // Fully-qualified class names to hide
    private final Set<String> hiddenClassNames;
    // Resource path prefixes to hide (e.g. "com/foo/internal/")
    private final Set<String> hiddenResourcePathPrefixes;
    // Extra predicate hook (optional)
    private final Predicate<Request> deny;

    private final boolean hideModuleInfo;

    @SuppressWarnings("unused")
	private FilteredClassLoader(Builder b) {
        super(b.parent);
        this.hiddenPackagePrefixes = Collections.unmodifiableSet(normalizePackages(b.hiddenPackagePrefixes));
        this.hiddenClassNames = Collections.unmodifiableSet(new HashSet<>(b.hiddenClassNames));
        this.hiddenResourcePathPrefixes = Collections.unmodifiableSet(normalizeResourcePrefixes(b.hiddenResourcePathPrefixes));
        this.deny = (b.deny == null) ? r -> false : b.deny;
        this.hideModuleInfo = b.hideModuleInfo;
    }

    /**
     * Denies (pretends not found) for hidden entries; otherwise delegates to normal parent-first behavior.
     * Because we block before delegation, we can "hide" classes even if they already exist/are loaded in the parent.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Objects.requireNonNull(name, "name");

        if (isHiddenClassName(name) || isHiddenByPackage(name) || deny.test(Request.forClass(name))) {
            throw new ClassNotFoundException(name);
        }

        // Normal parent-first behaviour (parent -> bootstrap -> findClass). We do not define classes ourselves.
        return super.loadClass(name, resolve);
    }

    /**
     * Resources can also leak visibility (ServiceLoader files, META-INF, etc).
     * This hides resources under hidden package/resource prefixes, and (optionally) module-info.class.
     */
    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name, "name");

        if (hideModuleInfo && "module-info.class".equals(name)) return null;

        if (isHiddenResource(name) || deny.test(Request.forResource(name))) {
            return null;
        }

        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name, "name");

        if (hideModuleInfo && "module-info.class".equals(name)) {
            return Collections.emptyEnumeration();
        }

        if (isHiddenResource(name) || deny.test(Request.forResource(name))) {
            return Collections.emptyEnumeration();
        }

        return super.getResources(name);
    }

    /**
     * Optional: avoid exposing a Package object for hidden packages.
     * (This is defensive; primary protection is in loadClass/getResource.)
     */
    @Override
    protected Package getPackage(String name) {
        if (name != null) {
            if (isHiddenPackageName(name) || deny.test(Request.forPackage(name))) {
                return null;
            }
        }
        return super.getPackage(name);
    }

    // ---------------------------
    // Matching / Normalization
    // ---------------------------

    private boolean isHiddenClassName(String className) {
        return hiddenClassNames.contains(className);
    }

    private boolean isHiddenByPackage(String className) {
        // className is "a.b.C"; package is "a.b"
        int idx = className.lastIndexOf('.');
        if (idx <= 0) return false;
        String pkg = className.substring(0, idx);
        return isHiddenPackageName(pkg);
    }

    private boolean isHiddenPackageName(String pkg) {
        // Hide if pkg == prefix OR pkg starts with prefix + "."
        for (String p : hiddenPackagePrefixes) {
            if (pkg.equals(p) || pkg.startsWith(p + ".")) return true;
        }
        return false;
    }

    private boolean isHiddenResource(String resourceName) {
        // Resource names are "/"-less (per ClassLoader contract), e.g. "com/foo/Bar.class".
        // Hide if matches resource prefix, or if it maps to a hidden package prefix.
        for (String rp : hiddenResourcePathPrefixes) {
            if (resourceName.startsWith(rp)) return true;
        }

        // If it's a class file, also check its package.
        if (resourceName.endsWith(".class") && resourceName.indexOf('/') >= 0) {
            String fqcn = resourceName.substring(0, resourceName.length() - ".class".length()).replace('/', '.');
            if (isHiddenClassName(fqcn) || isHiddenByPackage(fqcn)) return true;
        }

        return false;
    }

    private static Set<String> normalizePackages(Set<String> pkgs) {
        Set<String> out = new HashSet<>();
        for (String p : pkgs) {
            if (p == null) continue;
            String s = p.trim();
            if (s.isEmpty()) continue;
            // Accept "com.foo.*" as convenience
            if (s.endsWith(".*")) s = s.substring(0, s.length() - 2);
            // Remove trailing dots
            while (s.endsWith(".")) s = s.substring(0, s.length() - 1);
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private static Set<String> normalizeResourcePrefixes(Set<String> prefixes) {
        Set<String> out = new HashSet<>();
        for (String p : prefixes) {
            if (p == null) continue;
            String s = p.trim();
            if (s.isEmpty()) continue;

            // ClassLoader resource names should not start with "/"
            if (s.startsWith("/")) s = s.substring(1);

            // If someone passes a package name, allow it (convert '.' to '/')
            if (s.indexOf('/') < 0 && s.indexOf('.') >= 0) {
                s = s.replace('.', '/');
            }

            // Ensure trailing slash to mean "prefix directory"
            if (!s.endsWith("/")) s = s + "/";

            out.add(s);
        }
        return out;
    }

    // ---------------------------
    // Builder + predicate support
    // ---------------------------

    public static Builder builder(ClassLoader parent) {
        return new Builder(parent);
    }

    public static final class Builder {
        private final ClassLoader parent;

        private final Set<String> hiddenPackagePrefixes = new CopyOnWriteArraySet<>();
        private final Set<String> hiddenClassNames = new CopyOnWriteArraySet<>();
        private final Set<String> hiddenResourcePathPrefixes = new CopyOnWriteArraySet<>();

        private Predicate<Request> deny;
        private boolean hideModuleInfo;

        public Builder(ClassLoader parent) {
            // parent may be null (bootstrap) but most callers will pass system/app loader.
            this.parent = parent;
        }

        /** Hide a package and all its subpackages (e.g. "com.acme.internal"). Accepts "com.acme.internal.*" too. */
        public Builder hidePackage(String packagePrefix) {
            hiddenPackagePrefixes.add(packagePrefix);
            // also hide resources under that package path to reduce leaks
            if (packagePrefix != null) {
                String p = packagePrefix.trim();
                if (p.endsWith(".*")) p = p.substring(0, p.length() - 2);
                hiddenResourcePathPrefixes.add(p.replace('.', '/'));
            }
            return this;
        }

        /** Hide a single class (fully-qualified). */
        public Builder hideClass(String fqcn) {
            hiddenClassNames.add(fqcn);
            if (fqcn != null && fqcn.contains(".")) {
                String res = fqcn.replace('.', '/');
                // add resource prefix up to class file name to cover direct lookups
                hiddenResourcePathPrefixes.add(res.substring(0, res.lastIndexOf('/') + 1));
            }
            return this;
        }

        /** Hide resources by path prefix, e.g. "META-INF/services/" or "com/acme/internal/". */
        public Builder hideResourcePrefix(String resourcePathPrefix) {
            hiddenResourcePathPrefixes.add(resourcePathPrefix);
            return this;
        }

        /**
         * Optional: hide module descriptors by making module-info.class undiscoverable via getResource(s).
         * This does NOT stop ModuleLayer resolution; it just blocks resource-level discovery.
         */
        public Builder hideModuleInfo(boolean hide) {
            this.hideModuleInfo = hide;
            return this;
        }

        /**
         * Optional: advanced deny hook. Return true to pretend "not found".
         * Applies to class/package/resource requests.
         */
        public Builder denyIf(Predicate<Request> denyPredicate) {
            this.deny = denyPredicate;
            return this;
        }

        public FilteredClassLoader build() {
            return new FilteredClassLoader(this);
        }
    }

    /** Request type for the denyIf predicate. */
    public static final class Request {
        public enum Kind { CLASS, RESOURCE, PACKAGE }

        public final Kind kind;
        public final String name;

        private Request(Kind kind, String name) {
            this.kind = kind;
            this.name = name;
        }

        public static Request forClass(String fqcn)     { return new Request(Kind.CLASS, fqcn); }
        public static Request forResource(String path)  { return new Request(Kind.RESOURCE, path); }
        public static Request forPackage(String pkg)    { return new Request(Kind.PACKAGE, pkg); }
    }
}
