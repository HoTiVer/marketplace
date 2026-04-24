INSERT INTO public."user"(id, email, password, display_name)
VALUES
    (0, 'service@system.local', '', 'marketplace'),
    (1,'admin@gmail.com', '$2a$10$/UEXF5YfBUdODDaYxjx1feHmTdsnjD9GVTnmo.9RvHAxUNUA9No8W',
     'admin'),
    (2, 'mike90seller@gmail.com', '$2a$10$ofsxYv5X1IUmyX7CG.RDxuqw9YRksRVqeErrLHNYJLfOxPlw745qO', 'Mike'),
    (3, 'robert52@gmail.com', '$2a$10$wIVi6G2phnOYvIK.OCOHI.1NfnqB1acxS.Gwnv8VINaZFSFBEqaEK', 'Bob'),
    (4, 'oleg.rybak@example.com', '$2a$10$OqCbyL6Rp2pMQ/uk.CVaJe18.IJ1Jn2CIoKmnPgclamn.WFs4qWBS', 'Oleg Rybak'),
    (5, 'bestLibrary@gmail.com', '$2a$10$xuAn3e7vnMoN7ly8Q2tIte/mJu7Mlhn/.ZIVc3/KExMZGTEAFsJpO', 'Library'),
    (6, 'computerShop@gmail.com', '$2a$10$UARglgtfPcJjcZ0H7lmDQ.gFKsOxEO6eVmTJnI5xyN7bZIzWb0EBC', 'Pc shop'),
    (7, 'toyStore@gmail.com', '$2a$10$yW8orE5hYi4r.FcwQ.zfIOvNoNKVvJQAC4U5mivL6Ci6R9TwQ1K8i', 'Toy Store'),
    (8, 'gymShop@gmail.com', '$2a$10$SOPbXknQYtkq2Q.xH6AqgeSYLlvb7ras.M94jWdDw8cfHohAL.2uW', 'Gym'),
    (9, 'japaneseFood@gmail.com', '$2a$10$HfYkd6q3dgLOF.rFj3PNOOIlJ7o9g3TVB7ZK5kT3YUIY1y.8fqgK6', 'Food from Japan');


SELECT SETVAL('sequence_user', max(id)) FROM public."user";