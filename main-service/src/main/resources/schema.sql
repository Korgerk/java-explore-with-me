CREATE INDEX IF NOT EXISTS idx_event_category_state ON events(category_id, state);

CREATE INDEX IF NOT EXISTS idx_event_initiator ON events(initiator_id);

CREATE INDEX IF NOT EXISTS idx_request_event_status ON participation_requests(event_id, status);

CREATE INDEX IF NOT EXISTS idx_request_requester ON participation_requests(requester_id);

CREATE INDEX IF NOT EXISTS idx_event_date_state ON events(event_date, state);