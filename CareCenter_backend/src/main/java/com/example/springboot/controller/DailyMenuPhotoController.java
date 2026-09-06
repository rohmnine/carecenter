package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.common.UID;
import com.example.springboot.entity.DailyMenuPhoto;
import com.example.springboot.service.DailyMenuPhotoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 每日菜品图片Controller
 */
@RestController
@RequestMapping("/dailyMenuPhoto")
public class DailyMenuPhotoController {

    @Resource
    DailyMenuPhotoService dailyMenuPhotoService;

    @Value("${file.upload.path:#{null}}")
    private String uploadPath;

    /**
     * 上传菜品图片（管理员）
     */
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("files") MultipartFile[] files,
                            @RequestParam("photoDate") String photoDate,
                            @RequestParam("mealType") String mealType,
                            @RequestParam(value = "description", defaultValue = "") String description,
                            @RequestParam(value = "uploader", defaultValue = "") String uploader) {
        if (files == null || files.length == 0) {
            return Result.error("-1", "请选择要上传的图片");
        }

        // Determine upload directory
        String realPath;
        if (uploadPath != null && !uploadPath.isEmpty()) {
            realPath = uploadPath;
        } else {
            realPath = System.getProperty("user.dir") + "/CareCenter_backend/src/main/resources/files/";
        }

        File dir = new File(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        java.util.List<String> uploadedFilenames = new java.util.ArrayList<>();

        for (MultipartFile uploadFile : files) {
            if (uploadFile == null || uploadFile.isEmpty()) {
                continue;
            }

            // Generate unique filename
            String originalFilename = uploadFile.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = new UID().produceUID() + suffix;

            try {
                uploadFile.transferTo(new File(realPath + newFilename));
            } catch (IOException e) {
                e.printStackTrace();
                return Result.error("-1", "图片上传失败：" + e.getMessage());
            }

            // 每张图片保存一条记录，同一天同一餐次允许存在多张图片
            DailyMenuPhoto photo = new DailyMenuPhoto();
            photo.setPhotoDate(photoDate);
            photo.setMealType(mealType);
            photo.setPhotoUrl(newFilename);
            photo.setDescription(description);
            photo.setUploader(uploader);
            dailyMenuPhotoService.save(photo);
            uploadedFilenames.add(newFilename);
        }

        if (uploadedFilenames.isEmpty()) {
            return Result.error("-1", "请选择有效的图片文件");
        }

        return Result.success(uploadedFilenames);
    }

    /**
     * 获取指定日期的菜品图片
     */
    @GetMapping("/byDate")
    public Result<?> getByDate(@RequestParam String photoDate) {
        QueryWrapper<DailyMenuPhoto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("photo_date", photoDate);
        queryWrapper.orderByAsc("meal_type").orderByAsc("create_time").orderByAsc("id");
        List<DailyMenuPhoto> list = dailyMenuPhotoService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 获取今日菜品图片
     */
    @GetMapping("/today")
    public Result<?> getToday() {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        QueryWrapper<DailyMenuPhoto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("photo_date", today);
        queryWrapper.orderByAsc("meal_type").orderByAsc("create_time").orderByAsc("id");
        List<DailyMenuPhoto> list = dailyMenuPhotoService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 删除菜品图片
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        boolean result = dailyMenuPhotoService.removeById(id);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 获取文件访问路径（用于图片展示）
     */
    @GetMapping("/file/{filename}")
    public void getFile(@PathVariable String filename, javax.servlet.http.HttpServletResponse response) {
        String realPath;
        if (uploadPath != null && !uploadPath.isEmpty()) {
            realPath = uploadPath;
        } else {
            realPath = System.getProperty("user.dir") + "/CareCenter_backend/src/main/resources/files/";
        }

        File file = new File(realPath + filename);
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }

        response.setContentType("image/jpeg");
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            javax.servlet.ServletOutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            fis.close();
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
