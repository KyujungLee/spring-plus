package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final String title;

    private final Long countManagers;

    private final Long countComments;

    public TodoSearchResponse(String title, Long countManagers, Long countComments){
        this.title = title;
        this.countManagers = countManagers;
        this.countComments = countComments;
    }
}
