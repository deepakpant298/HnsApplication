package com.insider.hnstories.service;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.insider.hnstories.entity.Story;
import com.insider.hnstories.entity.TopComment;

public interface HnsService {
		List<Story> getTopStories();
		SortedSet<TopComment> getCommentsById(int storyId);
		Set<Story> getPastTopStories();
	}
