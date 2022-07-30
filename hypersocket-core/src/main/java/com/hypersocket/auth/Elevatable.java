package com.hypersocket.auth;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public interface Elevatable {

	default <T> T callAs(Callable<T> callable, Principal principal) throws Exception {
		setupSystemContext(principal);
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callAs(Callable<T> callable, Realm realm) throws Exception {
		setupSystemContext(realm);
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callAs(Callable<T> callable, Session session, Locale locale) throws Exception {
		setCurrentSession(session, locale);
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callAsSystemContext(Callable<T> callable) throws Exception {
		setupSystemContext();
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callAsSystemContext(Callable<T> callable, Realm realm) throws Exception {
		setupSystemContext(realm);
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callWithAuthenticatedContext(Callable<T> callable, Session session, Locale locale) throws Exception {
		setCurrentSession(session, locale);
		try {
			return callable.call();
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T callWithElevatedPermissions(Callable<T> callable, PermissionType... permissions) throws Exception {
		elevatePermissions(permissions);
		try {
			return callable.call();
		} finally {
			clearElevatedPermissions();
		}
	}

	@Deprecated
	void clearElevatedPermissions();

	@Deprecated
	void clearPrincipalContext();

	@Deprecated
	void elevatePermissions(PermissionType... permissions);

	default void runAs(Runnable runnable, Session session, Locale locale) {
		setCurrentSession(session, locale);
		try {
			runnable.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runAsSystemContext(Runnable r) {
		setupSystemContext();
		try {
			r.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runAsSystemContext(Runnable r, Realm realm) {
		setupSystemContext(realm);
		try {
			r.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runWithAuthenticatedContext(Runnable runnable, Session session, Locale locale) {
		setCurrentSession(session, locale);
		try {
			runnable.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runWithAuthenticatedContext(Runnable runnable, Session session, Realm realm, Locale locale) {
		setCurrentSession(session, realm, locale);
		try {
			runnable.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runWithAuthenticatedContext(Runnable runnable, Session session, Realm realm, Principal principal,
			Locale locale) {
		setCurrentSession(session, realm, locale);
		try {
			runnable.run();
		} finally {
			clearPrincipalContext();
		}
	}

	default void runWithElevatedPermissions(Runnable runnable, PermissionType... permissions) {
		elevatePermissions(permissions);
		try {
			runnable.run();
		} finally {
			clearElevatedPermissions();
		}
	}

	@Deprecated
	void setCurrentSession(Session session, Locale locale);

	@Deprecated
	void setCurrentSession(Session session, Realm realm, Locale locale);

	@Deprecated
	void setCurrentSession(Session session, Realm realm, Principal principal, Locale locale);

	@Deprecated
	void setupSystemContext();

	@Deprecated
	void setupSystemContext(Principal principal);

	@Deprecated
	void setupSystemContext(Realm realm);

	default <T> T silentlyCallAs(Callable<T> callable, Principal principal) {
		setupSystemContext(principal);
		try {
			try {
				return callable.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(String.format("Call as %s failed.", principal.getName()), e);
			}
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T silentlyCallAs(Callable<T> callable, Realm realm) {
		setupSystemContext(realm);
		try {
			try {
				return callable.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(String.format("Call as %s failed.", realm.getName()), e);
			}
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T silentlyCallAs(Callable<T> callable, Session session, Locale locale) {
		setCurrentSession(session, locale);
		try {
			try {
				return callable.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(String.format("Call as %s failed.", session.getName()), e);
			}
		} finally {
			clearPrincipalContext();
		}
	}

	default <T> T silentlyCallAsSystemContext(Callable<T> callable) {
		try {
			return callAsSystemContext(callable);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to run as system context.", e);
		}
	}

	default <T> T silentlyCallAsSystemContext(Callable<T> callable, Realm realm) {
		try {
			return callAsSystemContext(callable, realm);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to run as system context.", e);
		}
	}

	default <T> T silentlyCallWithElevatedPermissions(Callable<T> callable, PermissionType... permissions) {
		try {
			return callWithElevatedPermissions(callable, permissions);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to run as system context.", e);
		}
	}

	default Closeable tryAs(Principal principal) {
		setupSystemContext(principal);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}

	default Closeable tryAs(Realm realm) {
		setupSystemContext(realm);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}

	default Closeable tryAs(Session session) {
		return tryAs(session, Locale.getDefault());
	}

	default Closeable tryAs(Session session, Locale locale) {
		setCurrentSession(session, locale);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}

	default Closeable tryAs(Session session, Realm realm, Locale locale) {
		setCurrentSession(session, realm, locale);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}

	default Closeable tryAs(Session session, Realm realm, Principal principal, Locale locale) {
		setCurrentSession(session, realm, principal, locale);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}

	default Closeable tryWithElevatedPermissions(PermissionType... permissions) {
		elevatePermissions(permissions);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearElevatedPermissions();
			}
		};
	}

	default Closeable tryWithSystemContext() {
		setupSystemContext();
		return new Closeable() {
			@Override
			public void close() throws IOException {
				clearPrincipalContext();
			}
		};
	}
}
