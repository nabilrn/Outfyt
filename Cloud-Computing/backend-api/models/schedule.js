'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Schedule extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      this.hasMany(models.Recommendation, {
        foreignKey: "scheduleId",
        onUpdate: "CASCADE",
        onDelete: "CASCADE",
      });

      // Relasi belongs-to ke User melalui googleId
      this.belongsTo(models.User, {
        foreignKey: "googleId",
        onUpdate: "CASCADE",
        onDelete: "CASCADE",
      });
    }
  }
  Schedule.init({
    scheduleId: {
      allowNull: false,
      autoIncrement: true,
      primaryKey: true,
      type: DataTypes.INTEGER
    },
    googleId: DataTypes.STRING,
    title: DataTypes.STRING,
    date: DataTypes.STRING,
    desc: DataTypes.STRING,
    time: DataTypes.STRING,
    type: DataTypes.STRING,


    
  
  }, {
    sequelize,
    modelName: 'Schedule',
  });
  return Schedule;
};