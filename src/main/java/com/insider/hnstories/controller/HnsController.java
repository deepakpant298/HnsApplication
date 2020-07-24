package com.insider.hnstories.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.insider.hnstories.entity.Story;
import com.insider.hnstories.entity.TopComment;
import com.insider.hnstories.exceptions.NoRecordFoundException;
import com.insider.hnstories.service.HnsService;
import com.insider.hnstories.util.ApiConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class HnsController {

	
	@Autowired
	private HnsService hnsService;

	/**	
	 * Get the top 10 stories ranked by score in the last 10 minutes.
	 * 
	 */
	@GetMapping("/top-stories")
	public List<Story> getTopStories() {
		
		log.info("Getting top 10 stories");
		List<Story> topStories = hnsService.getTopStories();
		if (topStories.isEmpty()) {
			throw new NoRecordFoundException(ApiConstants.NO_STORY_FOUND_MESSAGE);
		}

		return topStories;
	}

	/**
	 * Get Top 10 parent comments on a given story A story has several comments and
	 * each comment has child comments. Return only the parent comments sorted by
	 * total number of comments
	 */
	@GetMapping("/comments/{storyId}")
	public List<TopComment> getTopComments(@PathVariable int storyId) {
		log.info("Getting top 10 comments for Story with id: {}", storyId);
		
		List<TopComment> topComments = hnsService.getCommentsById(storyId).stream().limit(10)
				.collect(Collectors.toList());

		if (topComments.isEmpty()) {
			throw new NoRecordFoundException(ApiConstants.NO_COMMENT_FOUND_MESSAGE);
		}

		return topComments;
	}

	/**
	 * Get past top stories served previously
	 */
	@GetMapping("/past-stories")
	public Set<Story> getPastStories() {
		log.info("Getting past served stories");
		Set<Story> pastStories = hnsService.getPastTopStories();
		if (pastStories.isEmpty()) {
			throw new NoRecordFoundException(ApiConstants.NO_STORY_FOUND_MESSAGE);
		}
		return pastStories;
	}
}
