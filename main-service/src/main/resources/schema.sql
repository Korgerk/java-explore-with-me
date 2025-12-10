-- При необходимости можно добавить кастомные индексы или ограничения

-- Индекс для поиска событий по категории и состоянию
CREATE INDEX IF NOT EXISTS idx_event_category_state ON events(category_id, state);

-- Индекс для поиска событий по инициатору
CREATE INDEX IF NOT EXISTS idx_event_initiator ON events(initiator_id);

-- Индекс для поиска запросов на участие по событию и статусу
CREATE INDEX IF NOT EXISTS idx_request_event_status ON participation_requests(event_id, status);

-- Индекс для поиска запросов на участие по пользователю
CREATE INDEX IF NOT EXISTS idx_request_requester ON participation_requests(requester_id);

-- Индекс для поиска событий по дате (для публичного API)
CREATE INDEX IF NOT EXISTS idx_event_date_state ON events(event_date, state);

-- Индекс для поиска по тексту (аннотация и описание)
CREATE INDEX IF NOT EXISTS idx_event_annotation ON events USING gin(to_tsvector('russian', annotation));
CREATE INDEX IF NOT EXISTS idx_event_description ON events USING gin(to_tsvector('russian', description));