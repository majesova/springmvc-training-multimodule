package vuzee.security.evaluators;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class VuzeePermissionEvaluator implements PermissionEvaluator{

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		// TODO Auto-generated method stub
		if ((authentication == null) || (targetDomainObject == null) || !(permission instanceof String)){
            return false;
        }
        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
         
        return hasPrivilege(authentication, targetType, permission.toString().toUpperCase());
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		if ((authentication == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }
        return hasPrivilege(authentication, targetType.toUpperCase(), 
          permission.toString().toUpperCase());
	}
	
	private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
	    for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
	        if (grantedAuth.getAuthority().startsWith(permission)) {
	            if (grantedAuth.getAuthority().contains(targetType)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}
}
