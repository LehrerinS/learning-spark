package com.teamtreehouse.courses1.model;

import java.util.List;
// this is an interface for Data Access Object

public interface CourseIdeaDAO {
    boolean add(CourseIdea idea);

    List<CourseIdea> findAll();

//    CourseIdea findBySlug(String slug);
}
