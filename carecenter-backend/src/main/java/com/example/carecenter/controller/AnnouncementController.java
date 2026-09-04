package com.example.carecenter.controller;

import com.example.carecenter.entity.Announcement;
import com.example.carecenter.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public List<Announcement> getAllAnnouncements() {
        return announcementService.getAllAnnouncements();
    }

    @GetMapping("/{id}")
    public Optional<Announcement> getAnnouncementById(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    @PostMapping
    public Announcement createAnnouncement(@RequestBody Announcement announcement) {
        return announcementService.createAnnouncement(announcement);
    }

    @DeleteMapping("/{id}")
    public void deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
    }
}