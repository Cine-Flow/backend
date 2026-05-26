UPDATE episodes
SET video_url = CASE 
    WHEN id % 10 = 0 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
    WHEN id % 10 = 1 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4'
    WHEN id % 10 = 2 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4'
    WHEN id % 10 = 3 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4'
    WHEN id % 10 = 4 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4'
    WHEN id % 10 = 5 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4'
    WHEN id % 10 = 6 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4'
    WHEN id % 10 = 7 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4'
    WHEN id % 10 = 8 THEN 'https://storage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4'
    ELSE 'https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4'
END
WHERE video_url IS NULL;
