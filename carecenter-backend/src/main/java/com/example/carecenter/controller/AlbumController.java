package com.example.carecenter.controller;

import com.example.carecenter.entity.Album;
import com.example.carecenter.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping
    public List<Album> getAllAlbums() {
        return albumService.getAllAlbums();
    }

    @GetMapping("/{id}")
    public Optional<Album> getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id);
    }

    @PostMapping
    public Album createAlbum(@RequestBody Album album) {
        return albumService.createAlbum(album);
    }

    @DeleteMapping("/{id}")
    public void deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
    }
}