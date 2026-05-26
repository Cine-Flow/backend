UPDATE episodes
SET video_url = CASE 
    WHEN id % 3 = 0 THEN 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4'
    WHEN id % 3 = 1 THEN 'https://www.w3schools.com/html/mov_bbb.mp4'
    WHEN id % 3 = 2 THEN 'https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_1MB.mp4'
END;
