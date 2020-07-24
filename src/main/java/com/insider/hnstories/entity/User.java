package com.insider.hnstories.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
	private String about;
	private int created;
	private int delay;
	private String id;
	private int karma;
	private int[] submitted;
}