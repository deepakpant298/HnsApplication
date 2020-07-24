package com.insider.hnstories.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class Comment implements Serializable {
	private static final long serialVersionUID = 1L;
	private String by;
	private int id;
	private int parent;
	private int[] kids;
	private String text;
	private int time;
}
