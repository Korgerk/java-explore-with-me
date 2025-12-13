package ru.practicum.ewm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestFactory {

    public static PageRequest from(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }

    public static PageRequest from(int from, int size, Sort sort) {
        int page = from / size;
        return PageRequest.of(page, size, sort);
    }
}
