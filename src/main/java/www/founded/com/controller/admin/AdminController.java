package www.founded.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.admin.AdminPostDTO;
import www.founded.com.model.register.UserRegister;
import www.founded.com.service.admin.AdminService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1) All accounts
    @GetMapping("/users")
    public ResponseEntity<List<UserRegister>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 2) All posts by a userRegisterId
    @GetMapping("/users/{userRegisterId}/posts")
    public ResponseEntity<List<AdminPostDTO>> getPosts(@PathVariable Long userRegisterId) {
        return ResponseEntity.ok(adminService.getAllPostsByUserRegisterId(userRegisterId));
    }

    // 3) Delete post (admin)
    @DeleteMapping("/posts")
    public ResponseEntity<Void> deletePost(
            @RequestParam String postType,
            @RequestParam Long postId
    ) {
        adminService.deletePost(postType, postId);
        return ResponseEntity.noContent().build();
    }

    // 4) Delete account (admin)
    @DeleteMapping("/users/{userRegisterId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userRegisterId) {
        adminService.deleteUserRegister(userRegisterId);
        return ResponseEntity.noContent().build();
    }
}
