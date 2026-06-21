-- V17__update_shorts_metadata.sql
-- Update metadata (titles and descriptions) for portrait shorts (IDs 201 to 207) to match the latest assets

-- ID 201: Pháo hoa đêm giao thừa (image_42119a.jpg)
UPDATE films SET title = 'Mãn nhãn với màn bắn pháo hoa rực rỡ đón năm mới', description = 'Khoảnh khắc pháo hoa bùng nổ trên bầu trời đêm cực kỳ lung linh và sống động. Bạn đã sẵn sàng cho năm mới chưa?' WHERE id = 201;
UPDATE episodes SET title = 'Mãn nhãn với màn bắn pháo hoa rực rỡ đón năm mới' WHERE film_id = 201 AND episode_number = 1;

-- ID 202: Thuyền chài quăng lưới (image_4211bb.jpg)
UPDATE films SET title = 'Bình yên sông nước với cảnh quăng lưới bắt cá', description = 'Hình ảnh người ngư dân thuần thục quăng lưới trên mạn thuyền giữa không gian bao la của biển trời lúc bình minh.' WHERE id = 202;
UPDATE episodes SET title = 'Bình yên sông nước với cảnh quăng lưới bắt cá' WHERE film_id = 202 AND episode_number = 1;

-- ID 203: Sóng biển vỗ vào vách đá (image_4211c2.jpg)
UPDATE films SET title = 'Cảnh biển xanh sóng vỗ rì rào vào vách đá hoang sơ', description = 'Chiêm ngưỡng vẻ đẹp hùng vĩ của thiên nhiên với làn nước biển xanh ngắt và những con sóng trắng xóa xô bờ.' WHERE id = 203;
UPDATE episodes SET title = 'Cảnh biển xanh sóng vỗ rì rào vào vách đá hoang sơ' WHERE film_id = 203 AND episode_number = 1;

-- ID 204: Cosplay nữ tiên tộc tóc hồng (image_4214c5.jpg)
UPDATE films SET title = 'Mê mẩn với chiếc nhan sắc cosplay anime cực đỉnh', description = 'Cận cảnh layout makeup và trang phục biến hình thành nàng tiên tóc hồng siêu dễ thương tại lễ hội hoa anh đào.' WHERE id = 204;
UPDATE episodes SET title = 'Mê mẩn với chiếc nhan sắc cosplay anime cực đỉnh' WHERE film_id = 204 AND episode_number = 1;

-- ID 205: Hòn đảo đá giữa biển (image_4214e0.jpg)
UPDATE films SET title = 'Khám phá hòn đảo đá độc đáo nổi lên giữa lòng biển khơi', description = 'Bãi biển cát trắng mịn màng ôm trọn lấy ngọn núi đá phủ đầy cây xanh tạo nên một bức tranh tuyệt tác.' WHERE id = 205;
UPDATE episodes SET title = 'Khám phá hòn đảo đá độc đáo nổi lên giữa lòng biển khơi' WHERE film_id = 205 AND episode_number = 1;

-- ID 206: Dắt chó đi dạo trời tuyết (image_421500.jpg)
UPDATE films SET title = 'Trải nghiệm dạo bước cùng chú cún cưng giữa rừng tuyết trắng', description = 'Khung cảnh mùa đông lãng mạn khi cùng người bạn bốn chân đi dạo dưới những tán thông phủ đầy tuyết trắng xóa.' WHERE id = 206;
UPDATE episodes SET title = 'Trải nghiệm dạo bước cùng chú cún cưng giữa rừng tuyết trắng' WHERE film_id = 206 AND episode_number = 1;

-- ID 207: Bến thuyền du lịch vùng biển nhiệt đới (image_4211a0.jpg)
UPDATE films SET title = 'Góc view triệu đô hướng ra vịnh biển đầy mộng mơ', description = 'Toàn cảnh bãi biển đầy nắng với những rặng dừa xanh mát và đoàn thuyền đánh cá neo đậu yên bình trên làn nước trong vắt.' WHERE id = 207;
UPDATE episodes SET title = 'Góc view triệu đô hướng ra vịnh biển đầy mộng mơ' WHERE film_id = 207 AND episode_number = 1;
