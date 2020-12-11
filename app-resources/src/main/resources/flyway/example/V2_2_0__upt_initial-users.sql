INSERT INTO oskari_roles(name, is_guest) values('UPTAdmin',false),('UPTUser',false) on conflict(name) do nothing;
-- insert admin user;
INSERT INTO oskari_users(user_name, first_name, last_name, email, uuid) VALUES('demoadmin', 'UPT', 'UPTAdmin', 'demoadmin@example.com','727704fd-9e40-4d89-a0dd-431fd0bcd25c') on conflict(user_name) do nothing;
INSERT INTO oskari_users(user_name, first_name, last_name, email, uuid) VALUES('demouser', 'UPT', 'UPTUser', 'demouser@example.com','ec29b4c6-ca3f-4325-b6ef-b69220142dda') on conflict(user_name) do nothing;

INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demoadmin'), (SELECT id FROM oskari_roles WHERE name = 'Admin'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demoadmin'), (SELECT id FROM oskari_roles WHERE name = 'User'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demoadmin'), (SELECT id FROM oskari_roles WHERE name = 'UPTAdmin'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demoadmin'), (SELECT id FROM oskari_roles WHERE name = 'UPTUser'));

INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demouser'), (SELECT id FROM oskari_roles WHERE name = 'User'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'demouser'), (SELECT id FROM oskari_roles WHERE name = 'UPTUser'));

-- use admin/oskari credentials for admin user (user_name in oskari_users must match login on oskari_jaas_users);
INSERT INTO oskari_jaas_users(login, password) VALUES('demoadmin', '$2a$10$UdiNl6IICcy0FAQWDsuyIePtD57ySqHqHOUARVBGOYFpwGSzWtJOq') , ('demouser', '$2a$10$UdiNl6IICcy0FAQWDsuyIePtD57ySqHqHOUARVBGOYFpwGSzWtJOq') on conflict(login) do update set password=excluded.password;