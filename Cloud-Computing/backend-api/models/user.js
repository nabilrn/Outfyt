"use strict";
const { Model } = require("sequelize");
module.exports = (sequelize, DataTypes) => {
  class User extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The models/index file will call this method automatically.
     */
    static associate(models) {
      // define association here
      this.hasMany(models.Schedule, {
        foreignKey: "googleId",
        onUpdate: "CASCADE",
        onDelete: "CASCADE",
      });
    }
  }
  User.init(
    {
      googleId: {
        allowNull: false,
        primaryKey: true,
        type: DataTypes.STRING,
      },
      displayName: DataTypes.STRING,
      email: DataTypes.STRING,
      photoUrl: DataTypes.STRING,
      faceImageUrl: DataTypes.STRING,
      colorType: DataTypes.STRING,
      gender: DataTypes.STRING,
      genderCategory: DataTypes.STRING,
      refreshTokenOauth: DataTypes.STRING,
    },
    {
      sequelize,
      modelName: "User",
    }
  );
  return User;
};