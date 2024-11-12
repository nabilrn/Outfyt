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
      refreshTokenOauth: DataTypes.STRING,
    },
    {
      sequelize,
      modelName: "User",
    }
  );
  return User;
};