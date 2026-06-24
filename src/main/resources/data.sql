-- Seed invite codes (idempotente: só insere se o código ainda não existe)
INSERT INTO invite_codes (id, code, ativo)
SELECT '00000000-0000-0000-0001-000000000001', 'FEITU-ROGER', TRUE
WHERE NOT EXISTS (SELECT 1 FROM invite_codes WHERE code = 'FEITU-ROGER');

INSERT INTO invite_codes (id, code, ativo)
SELECT '00000000-0000-0000-0001-000000000002', 'FEITU-INVITE2', TRUE
WHERE NOT EXISTS (SELECT 1 FROM invite_codes WHERE code = 'FEITU-INVITE2');

INSERT INTO invite_codes (id, code, ativo)
SELECT '00000000-0000-0000-0001-000000000003', 'FEITU-INVITE3', TRUE
WHERE NOT EXISTS (SELECT 1 FROM invite_codes WHERE code = 'FEITU-INVITE3');
