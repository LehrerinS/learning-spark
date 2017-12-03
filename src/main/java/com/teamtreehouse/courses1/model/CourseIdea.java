package com.teamtreehouse.courses1.model;


import com.github.slugify.Slugify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseIdea {
    private String slug;
    private String title;
    private String creator;
    private Set<String> voters;

    public CourseIdea(String title, String creator) {
        voters = new HashSet<>();
        this.title = title;
        this.creator = creator;
        try {
            Slugify slugify = new Slugify();
//            TODO: this is very nice once you work with regex and you want to clean it. there is Normalizer class and check normalize and replaceAll methods.
//            TODO: this slugify method makes it clear text, without any spaces or special characters.
            slug = slugify.slugify(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public String getSlug() {
        return slug;
    }

    public boolean addVoter(String voterUserName){
        return voters.add(voterUserName);
    }

    public int getVoteCount(){
        return voters.size();
    }

    public List<String> getVoters() {
        return new ArrayList<>(voters);
    }

//    TODO: check with Ali what does it do exactly equals and hashcode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseIdea that = (CourseIdea) o;

        if (!title.equals(that.title)) return false;
        return creator.equals(that.creator);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + creator.hashCode();
        return result;
    }
}
