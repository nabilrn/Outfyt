const { User } = require('../models');

// Fungsi untuk memberikan rekomendasi warna berdasarkan kategori musim dan colorType
const getColorRecommendations = (colorType) => {
  // Warna rekomendasi untuk tiap musim
  const colorRecommendations = {
    fall: ["#D2691E", "#8B4513", "#A52A2A", "#B8860B", "#CD5C5C", "#FF6347", "#FF4500", "#2E8B57", "#8B0000", "#C71585"],
    spring: ["#98FB98", "#00FF7F", "#7CFC00", "#32CD32", "#228B22", "#9ACD32", "#FFD700", "#F0E68C", "#D2691E", "#FF4500"],
    summer: ["#1E90FF", "#87CEEB", "#4682B4", "#00BFFF", "#ADD8E6", "#B0E0E6", "#A9A9A9", "#808080", "#A9A9A9", "#C0C0C0"],
    winter: ["#F0F8FF", "#ADD8E6", "#B0C4DE", "#4682B4", "#5F9EA0", "#2F4F4F", "#708090", "#C0C0C0", "#A9A9A9", "#D3D3D3"]
  };

  // Mengembalikan rekomendasi warna berdasarkan colorType
  return colorRecommendations[colorType] || [];
};

// Endpoint untuk mendapatkan personal color dan rekomendasi warna
const personalColor = async (req, res) => {
  try {
    // Ambil data user berdasarkan googleId atau kriteria lainnya
    const user = await User.findOne({
      where: { googleId: req.user.googleId }, // Sesuaikan dengan pengidentifikasi user
      attributes: ['faceImageUrl', 'colorType', 'genderCategory'],
    });

    // Jika user tidak ditemukan
    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    // Ambil kategori musim dari colorType
    const colorType = user.colorType.toLowerCase();  // Misalnya: "fall", "spring", "summer", "winter"
    
    // Dapatkan rekomendasi warna berdasarkan colorType
    const colorRecommendations = getColorRecommendations(colorType);

    // Kirimkan response dengan data user dan rekomendasi warna
    return res.status(200).json({
      faceImageUrl: user.faceImageUrl,
      colorType: user.colorType,
      genderCategory: user.genderCategory,
      recommendedColors: colorRecommendations,
    });
  } catch (error) {
    console.error('Error fetching personal color data:', error);
    return res.status(500).json({ error: 'Server error' });
  }
};

module.exports = {
  personalColor,
};
