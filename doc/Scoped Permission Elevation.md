# Scoped Permission Elevation

As from version 2.4, a new way to raise the authenticated context and elevate permissions has been introduced. This replaces the copy/pasted pattern of using `setupSystemContext()` and friends in a `try/catch/finally` block.

Instead, you will now use `try-with-resource`, `Callable`, `Runnable` and the `@AuthenticatedContext` annotation.

The whole point is to reduce code, and to always ensure that code that elevates permissions only does so within a well defined scope, and that this is enforced by the framework.

Exactly which one is best depends on the context it is being used and the `Exception` requirements (both of the code within and the exception signature of the enclosing scope).

## Annotations

### @SystemContextRequired Annotation

This has been removed and it's functionality moved to `@AuthenticatedContext`. While it did a similar job, it could only work for system context, only for `@Service` implementations, and was used in very few places. I suspect that due to it's lack of visibility, lack of enforcement, and our tendency to copy and paste existing code and/or use the plugin generator (which does not output code with these annotations), it was sadly underused. This commit addresses that by enforcing usage of the new methods.

### @AuthenticationRequired Annotation

While related, this annotation does not change it's function, it still exists and is used in the same way and places. The only other thing that can be said about it, is it runs before `@AuthenticatedContext`.

### @AuthenticatedContext Annotation

The annotation is for use in `@Controller` and `@Service` implementations. 

 * `boolean system() default false;`	
 * `boolean realmHost() default false;`
 * `boolean currentRealmOrDefault() default false;`
 * `boolean preferActive() default false;`

Note that use in `@Service` implementations is currently restricted to running in system context. This is because the web request is unknown at this point (and it may be a non-web request anyway). For custom behaviour use the programmatic options such as `try-with-resource` or `Callable` and `Runnable`.

For example, take the simplest typical use case of an API call in a `@Controller` that must run the context of currently signed on user.

It currently looks like this.

```java
@AuthenticationRequired
@RequestMapping(value = "roles/role/{id}", method = RequestMethod.GET, produces = { "application/json" })
@ResponseBody
@ResponseStatus(value = HttpStatus.OK)
public Role getRole(HttpServletRequest request,
		HttpServletResponse response, @PathVariable("id") Long id)
		throws AccessDeniedException, UnauthorizedException,
		ResourceNotFoundException, SessionTimeoutException {

	setupAuthenticatedContext(sessionUtils.getSession(request),
			sessionUtils.getLocale(request));

	try {
		return permissionService.getRoleById(id,
				sessionUtils.getCurrentRealm(request));
	} finally {
		clearAuthenticatedContext();
	}
}
```

This can now be replaced with the following, reducing it to just pure implementation of the API call.

```java
@AuthenticationRequired
@RequestMapping(value = "roles/role/{id}", method = RequestMethod.GET, produces = { "application/json" })
@ResponseBody
@ResponseStatus(value = HttpStatus.OK)
@AuthenticatedContext
public Role getRole(HttpServletRequest request,
		HttpServletResponse response, @PathVariable("id") Long id)
		throws AccessDeniedException, UnauthorizedException,
		ResourceNotFoundException, SessionTimeoutException {

	return permissionService.getRoleById(id,
			sessionUtils.getCurrentRealm(request));
}
```

You might want to run the block of code as a system administrator. Simply add `system = true` to the annotation. 

```java

	
@RequestMapping(value = "passwordPolicys/generate/{id}/{length}", method = RequestMethod.GET, produces = { "application/json" })
@ResponseBody
@ResponseStatus(value = HttpStatus.OK)
@AuthenticatedContext(system = true)
public ResourceStatus<String> generatePassword(
		HttpServletRequest request, HttpServletResponse response, @PathVariable Long id, @PathVariable Integer length)
		throws AccessDeniedException, UnauthorizedException,
		SessionTimeoutException {

	try {
		PasswordPolicyResource policy = resourceService.getResourceById(id);
		return new ResourceStatus<String>(resourceService.generatePassword(policy, length));
	} catch (ResourceNotFoundException e) {
		return new ResourceStatus<String>(false, e.getMessage());
	}
	
}
```

For the above 2 cases, you can also add the `preferActive = true` attribute to the annotation, which indicates the current session should be used if there is one present.

Another possible use-case is to run in the context of the current realm, based on the `Server-Name` header. There are two variations of this. 

The first effectively replaces `setupSystemContext(realmService.getRealmByHost(request.getServerName()));`. It will always run the code as a system administrator of the detected run. You simply add `realmHost = true` to the `@AuthenticatedContext` attribute.

The second variant is similar, but will use the currently authenticated realm if the user is already authenticated. If they are not, the system realm will be used. In this case, add `currentRealmOrDefault = true`.

### Customised Behaviour

If the annotation does not meet your requirements, you can always use the patterns below intended for non-controller code to perform custom behaviour.

## Non-Spring Classes

For classes that are not managed by dependency injection, or anywhere where you want custom behaviour, there are now several new methods to help run blocks of code.

These are all contained within the new `Elevatable` interface, which is implemented by `AuthenticatedController` and `AuthenticatedService`. This interface provides a suite of `default` implementations, with a core of 6 methods that must be implemented should there ever be a need to provide this interface outside of these two classes (I see no reason to do this yet).

Because `AuthenticatedController` and `AuthenticatedService` both implement `Elevatable`, when working in classes that extend these you can just type ...

```java
	tryAsSystemContext(..);	
```

If you are in some other code that does not ultimately extend `Elevatable`, then you'll need a reference to any service that does. 

```java
	realmService.tryAsSystemContext(...);
```

## Try-With-Resource

This is probably the most concise way to use, but it does require that `IOException` is handled somewhere (due to use of `Closeable`). It's handy because it's just a `try` scope, so you can assign variables outside of the try and return values etc with no special consideration.

If `IOException` is thrown to higher up ..

```java
try(var c = elevatable.tryWithSystemContext()) {
	// Do some code that requires system permissions.
}
```

.. or if you need to wrap and re-throw it yourself ..

```java
try(var c = elevatable.tryWithSystemContext()) {
	// Do some code that requires system permissions.
}
catch(IOException ioe) {
	throw new IllegalStateException("Failed to X.", ioe);
}
```

There are several `tryAs*()` like methods.

 * `Closeable tryAs(Principal principal)`
 * `Closeable tryAs(Realm realm)`
 * `Closeable tryAs(Session session)`
 * `Closeable tryAs(Session session, Locale locale)`
 * `Closeable tryAs(Session session, Realm realm, Locale locale)`
 * `Closeable tryAs(Session session, Realm realm, Principal principal, Locale locale)`
 * `Closeable tryWithSystemContext()`
 * `Closeable tryWithElevatedPermissions(PermissionType... permissions)`

## Runnable

If the code you wish to run either throws no exceptions, or only unchecked exceptions, then `runAs()` methods are appropriate.

As it is effectively an inner class method invocation with no return value, you can only only effect class member variables. 

```java
elevatable.runAsSystemContext(() -> doSomething());
```

.. or ..

```java
elevatable.runAs(() -> {
	doSomething();
	doSomethingElse();
	someClassVariable = 123.0;
});
```

There are several `runAs*()` like methods.

 * `void runAs(Runnable runnable, Session session, Locale locale)`
 * `void runAsSystemContext(Runnable r)`
 * `void runAsSystemContext(Runnable r, Realm realm)`
 * `void runWithAuthenticatedContext(Runnable runnable, Session session, Locale locale) `
 * `void runWithAuthenticatedContext(Runnable runnable, Session session, Realm realm, Locale locale)`
 * `void runWithElevatedPermissions(Runnable runnable, PermissionType... permissions)`

## Callable

Similar to `Runnable`, you are using an inner class implementation, but in this case you have a typed return value, and `Exception` is thrown.

```java
var booleanValue = elevatable.callAsSystemContext(() -> {
	return configurationService.getBooleanValue("someProperty");
});
```

`Callable.call()` throws `Exception`. So while you do no need any exception handling inside the scope, you do will need to deal with  `Exception` outside of it. 

Using `callAs*()` like methods in a controller is easy, because you can just change the controller method to throw Exception too, but usage in services and other classes can be more annoying having to catch and sink or wrap Exception. It is still very slightly shorter than the existing method, and still safer than manually elevating and lowering permissions manually.

```java 
try {
	var booleanValue = elevatable.callAsSystemContext(() -> {
		return configurationService.getBooleanValue("someProperty");
	});
} catch(Exception e) {
	throw new IllegalStateException("Something bad happened.", e);
}
```

To make this easier, there also variants of `callAs*()` methods that catch the `Exception` for you and turn it into an unchecked `IllegalStateException`. 

There are several `callAs*()` like methods.

 * `<T> T callAs(Callable<T> callable, Principal principal) throws Exception`
 * `<T> T callAs(Callable<T> callable, Realm realm) throws Exception`
 * `<T> T callAs(Callable<T> callable, Session session, Locale locale) throws Exception`
 * `<T> T callAsSystemContext(Callable<T> callable) throws Exception`
 * `<T> T callAsSystemContext(Callable<T> callable, Realm realm) throws Exception`
 * `<T> T callWithAuthenticatedContext(Callable<T> callable, Session session, Locale locale) throws Exception`
 * `<T> T callWithElevatedPermissions(Callable<T> callable, PermissionType... permissions) throws Exception`
 * `<T> T silentlyCallAs(Callable<T> callable, Principal principal)`
 * `<T> T silentlyCallAs(Callable<T> callable, Realm realm)`
 * `<T> T silentlyCallAs(Callable<T> callable, Session session, Locale locale)`
 * `<T> T silentlyCallAsSystemContext(Callable<T> callable)`
 * ` <T> T silentlyCallAsSystemContext(Callable<T> callable, Realm realm)`
 * `<T> T silentlyCallWithElevatedPermissions(Callable<T> callable, PermissionType... permissions)`












