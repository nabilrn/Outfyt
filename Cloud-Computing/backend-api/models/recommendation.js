'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Recommendation extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      this.belongsTo(models.Schedule, {
        foreignKey: "scheduleId",
        onUpdate: "CASCADE",
        onDelete: "CASCADE",
      });
    }
  }
  Recommendation.init({
    recommendationId: {
      allowNull: false,
      autoIncrement: true,
      primaryKey: true,
      type: DataTypes.INTEGER
    },
    scheduleId: DataTypes.STRING,
    displayName: DataTypes.STRING,
    subCategory: DataTypes.STRING
  }, {
    sequelize,
    modelName: 'Recommendation',
  });
  return Recommendation;
};