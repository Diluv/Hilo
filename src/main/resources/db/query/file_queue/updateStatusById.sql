UPDATE project_file_queue
SET status             = 'running',
    status_change_time = NOW()
WHERE id = ?;