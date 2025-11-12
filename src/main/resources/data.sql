-- 태그 데이터 (이미 있을 경우 삽입 안 함)
INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '만남/동행') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='만남/동행');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '정보/공유') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='정보/공유');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '질문/요청') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='질문/요청');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '분실물') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='분실물');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '굿즈/이벤트') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='굿즈/이벤트');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '푸드/맛집') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='푸드/맛집');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '잡담/일상') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='잡담/일상');

INSERT INTO tag (tag_name)
SELECT * FROM (SELECT '기타') AS tmp
WHERE NOT EXISTS (SELECT tag_name FROM tag WHERE tag_name='기타');


-- 축제 데이터 (없을 때만 삽입)
INSERT INTO festival (name, slug)
SELECT * FROM (SELECT '4호선톤', 'line4thon') AS tmp
WHERE NOT EXISTS (SELECT name FROM festival WHERE name='4호선톤');
