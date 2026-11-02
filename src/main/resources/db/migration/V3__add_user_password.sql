ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);

UPDATE users SET password_hash = '$2a$10$ZMGrp6gfmLuQPcrGzrXpgO..XtKuFNQeATRrUFTSz0VK7YfvBYsW6' WHERE email = 'mate@etf.rs';
UPDATE users SET password_hash = '$2a$10$DTePMaIfTPKnOY5956I5ROLtlzxzcGRTN6hO8yi3ZfNTkK8cemU2K' WHERE email = 'ana@etf.rs';
UPDATE users SET password_hash = '$2a$10$FRMVXXX1LTzRn8FRcP6Ylema9rQ6Jjy0O0tne08D97pX0K0mGnbk2' WHERE email = 'petar@etf.rs';
UPDATE users SET password_hash = '$2a$10$4qDMKtILf2WpuDRu/FH9UuQFJejpRpRX8wZ5Bx.WfdAPSzPnz/hRC' WHERE email = 'admin@etf.rs';

ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;