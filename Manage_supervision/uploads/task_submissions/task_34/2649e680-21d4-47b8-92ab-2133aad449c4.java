// 文件路径: src/main/java/com/food/controller/RecipeController.java
package com.food.controller;

import com.food.model.Comment;
import com.food.model.Recipe;
import com.food.model.User;
import com.food.service.CommentService;
import com.food.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class RecipeController {
    // 图片上传路径
    private static final String RECIPE_UPLOAD_DIR = "src/main/webapp/uploads/recipes";
    
    @Autowired
    private RecipeService recipeService;
    
    @Autowired
    private CommentService commentService;

    // 用户查看菜谱列表
    @GetMapping("/user/home")
    public String userHome(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("recipes", recipeService.searchRecipes(search));
            model.addAttribute("searchKeyword", search);
        } else {
            model.addAttribute("recipes", recipeService.getAllRecipes());
        }
        return "user/home";
    }

    // 查看菜谱详情
    @GetMapping("/recipes/{id}")
    public String viewRecipe(@PathVariable Long id, Model model, HttpSession session) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null) {
            return "redirect:/";
        }
        
        // 使用树形结构获取评论
        List<Comment> commentTree = commentService.getCommentTreeByRecipe(recipe);
        
        model.addAttribute("recipe", recipe);
        model.addAttribute("comments", commentTree);
        model.addAttribute("user", session.getAttribute("user")); // 传递当前用户到视图
        
        return "recipe/view";
    }

    // 用户：我的菜谱列表页面
    @GetMapping("/user/recipes")
    public String userRecipes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("recipes", recipeService.getUserRecipes(user));
        return "user/recipes";
    }

    // 用户：新建菜谱页面
    @GetMapping("/user/recipes/new")
    public String newUserRecipeForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("isAdmin", false);
        return "recipe/form";
    }

    // 用户：编辑菜谱页面
    @GetMapping("/user/recipes/{id}/edit")
    public String editUserRecipeForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null || !recipeService.hasPermission(user, id)) {
            return "redirect:/user/recipes";
        }
        
        model.addAttribute("recipe", recipe);
        model.addAttribute("isAdmin", false);
        return "recipe/form";
    }
    
    // 上传菜谱图片
    @PostMapping(value = "/user/recipe/upload-image", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ImageUploadResponse uploadRecipeImage(
            @RequestParam("image") MultipartFile file,
            HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return new ImageUploadResponse(false, "用户未登录", null);
        }
        
        try {
            // 确保上传目录存在
            File uploadDir = new File(RECIPE_UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(RECIPE_UPLOAD_DIR, fileName);
            
            // 保存文件
            Files.write(filePath, file.getBytes());
            
            // 返回图片URL
            String imageUrl = "/uploads/recipes/" + fileName;
            
            return new ImageUploadResponse(true, "图片上传成功", imageUrl);
        } catch (IOException e) {
            return new ImageUploadResponse(false, "图片上传失败：" + e.getMessage(), null);
        }
    }

    // 用户：创建或更新菜谱
    @PostMapping("/user/recipes/save")
    public String saveUserRecipe(
            @ModelAttribute Recipe recipe,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // 如果是管理员，拒绝保存
        if (user.getRole() == User.UserRole.ADMIN) {
            return "redirect:/admin/recipes";
        }
        
        // 处理图片上传
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 确保上传目录存在
                File uploadDir = new File(RECIPE_UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // 生成唯一文件名
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(RECIPE_UPLOAD_DIR, fileName);
                
                // 保存文件
                Files.write(filePath, imageFile.getBytes());
                
                // 设置图片URL
                recipe.setImageUrl("/uploads/recipes/" + fileName);
            } catch (IOException e) {
                // 记录错误并继续
                System.err.println("菜谱图片上传失败：" + e.getMessage());
            }
        }
        
        // 如果是新建菜谱，设置创建者
        if (recipe.getId() == null) {
            recipe.setCreatedBy(user);
            recipeService.createRecipe(recipe);
        } else {
            // 编辑菜谱，需要检查权限
            if (recipeService.hasPermission(user, recipe.getId())) {
                // 保持原有创建者不变
                Recipe originalRecipe = recipeService.getRecipeById(recipe.getId());
                if (originalRecipe != null) {
                    // 如果没有上传新图片，保留原来的图片URL
                    if ((imageFile == null || imageFile.isEmpty()) && recipe.getImageUrl() == null) {
                        recipe.setImageUrl(originalRecipe.getImageUrl());
                    }
                    recipe.setCreatedBy(originalRecipe.getCreatedBy());
                    recipeService.updateRecipe(recipe);
                }
            } else {
                return "redirect:/user/recipes";
            }
        }
        
        return "redirect:/user/recipes";
    }

    // 用户：删除菜谱
    @PostMapping("/user/recipes/{id}/delete")
    @ResponseBody
    public String deleteUserRecipe(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "unauthorized";
        }
        
        if (!recipeService.hasPermission(user, id)) {
            return "unauthorized";
        }
        
        recipeService.deleteRecipe(id);
        return "success";
    }

    // 管理员：菜谱管理页面
    @GetMapping("/admin/recipes")
    public String adminRecipes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("recipes", recipeService.getAllRecipes());
        return "admin/recipes";
    }

    // 管理员：编辑菜谱页面 - 移除这个功能，管理员不能编辑菜谱
    // 但保留查看功能
    @GetMapping("/admin/recipes/{id}")
    public String viewAdminRecipe(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        return "redirect:/recipes/" + id;
    }

    // 管理员：删除菜谱
    @PostMapping("/admin/recipes/{id}/delete")
    @ResponseBody
    public String deleteAdminRecipe(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "unauthorized";
        }
        recipeService.deleteRecipe(id);
        return "success";
    }
}

// 图片上传响应类
class ImageUploadResponse {
    private boolean success;
    private String message;
    private String imageUrl;
    
    public ImageUploadResponse(boolean success, String message, String imageUrl) {
        this.success = success;
        this.message = message;
        this.imageUrl = imageUrl;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 