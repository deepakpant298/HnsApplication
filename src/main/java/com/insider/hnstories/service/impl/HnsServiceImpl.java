package com.insider.hnstories.service.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.insider.hnstories.entity.Comment;
import com.insider.hnstories.entity.Story;
import com.insider.hnstories.entity.TopComment;
import com.insider.hnstories.exceptions.InternalServerException;
import com.insider.hnstories.exceptions.NoRecordFoundException;
import com.insider.hnstories.service.CacheService;
import com.insider.hnstories.service.HnsService;
import com.insider.hnstories.util.ApiConstants;
import com.insider.hnstories.util.Utils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class HnsServiceImpl implements HnsService {

	@Autowired
	CacheService cacheService;

	/**
	 * Set to store all past stories served
	 */
	private static Set<Story> pastStorySet = new HashSet<>();

	/**
	 * Get the Top 10 stories
	 */
	public List<Story> getTopStories() {
		List<Story> topStoriesList;
		/**
		 * Check the existence of records in cache, if there are no records, fetch from
		 * HN API and add to cache
		 */
		if (cacheService.get(ApiConstants.TOP_STORIES_CACHE_NAME) == null) {
			topStoriesList = getTopStoriesList();
			try {
				cacheService.set(ApiConstants.TOP_STORIES_CACHE_NAME,
						Utils.getObjectMapper().writeValueAsString(topStoriesList));
			} catch (JsonProcessingException e) {
				log.error(e.toString());
				throw new InternalServerException();
			}
		} else {
			try {
				topStoriesList = Utils.getObjectMapper().readValue(
						cacheService.get(ApiConstants.TOP_STORIES_CACHE_NAME).toString(),
						new TypeReference<List<Story>>() {
						});
			} catch (Exception e) {
				log.error(e.toString());
				throw new InternalServerException();
			}
		}

		return topStoriesList;

	}

	/**
	 * Get all the previously served top stories
	 */
	public Set<Story> getPastTopStories() {
		return pastStorySet;
	}

	/**
	 * Get top parent comments for a provided storyId
	 */
	public SortedSet<TopComment> getCommentsById(int storyId) {
		/**
		 * Sorted set for Top comments in decreasing order
		 */
		SortedSet<TopComment> topCommentSet = new TreeSet<>(Comparator.comparing(TopComment::getTotalComments)
				.reversed().thenComparing(TopComment::getTotalComments));

		/**
		 * Get the story data
		 */
		Story story = Utils.getStory(storyId);
		if (story != null) {

			int[] comments = story.getKids();

			/**
			 * For each comment in the story, get total comments
			 */
			if (comments != null) {
				for (int commentId : comments) {

					Comment comment = Utils.getComment(commentId);
					if (comment != null) {
						
						int commentCount = 1 + Utils.getCommentCount(comment);

						TopComment topComment = new TopComment(comment.getText(), comment.getBy(),
								Utils.getUserAgeByName(comment.getBy()), commentCount);

						topCommentSet.add(topComment);
					}
				}
			} else {
				throw new NoRecordFoundException(ApiConstants.NO_COMMENT_FOUND_FOR_THE_STORY_MESSAGE);
			}
		} else {
			throw new NoRecordFoundException(ApiConstants.NO_STORY_FOUND_MESSAGE);
		}

		return topCommentSet;
	}

	/**
	 * Get top 10 stories
	 */
	private List<Story> getTopStoriesList() {

		SortedSet<Story> topStoriesSet = new TreeSet<>(
				Comparator.comparing(Story::getScore).reversed().thenComparing(Story::getScore));

		List<Integer> topStories = Utils.getTopStoryIds();
		int division = topStories.size() / 4;

		/**
		 * To get faster results, fetch data using 4 threads by spliting the list into 4
		 * sublists
		 */
		ExecutorService executor = Executors.newFixedThreadPool(4);
		CountDownLatch latch = new CountDownLatch(4);

		try {
			for (int i = 0; i < 4; i++) {
				int listSizeForThreadFrom = i * division;
				int listSizeForThreadTo = (i + 1) * division;
				executor.submit(() -> {
					addStoriesToSet(topStoriesSet, topStories.subList(listSizeForThreadFrom, listSizeForThreadTo));
					latch.countDown();
				});
			}
			latch.await();
		} catch (InterruptedException e) {
			log.error(e.toString());
			Thread.currentThread().interrupt();
			throw new InternalServerException();
		} finally {
			executor.shutdown();
		}

		/**
		 * To get the elements in decreasing order and not in random order, collect it
		 * into a list
		 */
		List<Story> topTenStories = topStoriesSet.stream().limit(10).collect(Collectors.toList());

		addToPastStorySet(topTenStories);

		return topTenStories;
	}

	/**
	 * Add each story to the SortedSet
	 */
	private void addStoriesToSet(SortedSet<Story> topStoriesSet, List<Integer> subList) {
		for (int storyId : subList) {
			Story story = Utils.getStory(storyId);
			if (story.getType().equals(ApiConstants.ITEM_TYPE_STORY)) {
				topStoriesSet.add(story);
			}
		}
	}

	/**
	 * Add top 10 stories to the pastStory set if it does not contains that story,
	 * If it contains that story but score is now updated. In Story.java: equals()
	 * is overridden to compare objects excluding score and kids because they can
	 * change
	 */
	private void addToPastStorySet(List<Story> topTenStories) {

		topTenStories.stream().forEach(topStory -> {
			if (pastStorySet.contains(topStory)) {
				pastStorySet.stream().filter(pastStory -> pastStory.equals(topStory)).findFirst()
						.ifPresent(story -> story.setScore(topStory.getScore()));
			} else {
				pastStorySet.add(topStory);
			}
		});
	}

}