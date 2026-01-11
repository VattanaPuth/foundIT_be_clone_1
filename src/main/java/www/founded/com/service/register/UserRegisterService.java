package www.founded.com.service.register;

import www.founded.com.model.register.UserRegister;

public interface UserRegisterService {
	UserRegister saveUser(UserRegister userRegister);
	UserRegister findOrCreateFromGoogle(String email, String googleSubject);
}
