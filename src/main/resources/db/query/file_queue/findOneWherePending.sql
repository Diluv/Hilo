SELECT *
FROM project_file_queue
WHERE status = 'pending'
ORDER BY created_at
LIMIT ?;