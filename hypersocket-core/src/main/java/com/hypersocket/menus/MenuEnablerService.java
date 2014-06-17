package com.hypersocket.menus;

import com.hypersocket.realm.Principal;

public interface MenuEnablerService {

	boolean enableMenu(String menu, Principal principal);
}
