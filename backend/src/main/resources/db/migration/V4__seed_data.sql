INSERT INTO users (id, name, email, password) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Test User', 'test@example.com', '$2a$12$1SkwhFte5ZG5i/YKnzSy.OMkFNBnpiOX2mT2pCBg5CokAHjYzFyv.');

INSERT INTO projects (id, name, description, owner_id) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Sample Project', 'A sample project for testing', 'a0000000-0000-0000-0000-000000000001');

INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, creator_id, due_date) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Task 1', 'First task', 'todo', 'high', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', CURRENT_DATE + INTERVAL '5 days'),
    ('c0000000-0000-0000-0000-000000000002', 'Task 2', 'Second task', 'in_progress', 'medium', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', CURRENT_DATE + INTERVAL '10 days'),
    ('c0000000-0000-0000-0000-000000000003', 'Task 3', 'Third task', 'done', 'low', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', NULL);
