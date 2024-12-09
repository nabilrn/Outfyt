'use strict';
/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('Recommendations', {
      recommendationId: {
        allowNull: false,
        primaryKey: true,
        type: Sequelize.STRING
      },
      scheduleId: {
        type: Sequelize.STRING,
        references: {
          model: 'Schedules', // Nama tabel yang diacu
          key: 'scheduleId' // Kolom pada tabel Users
        },
    
      },
      displayName: {
        type: Sequelize.STRING
      },
      subCategory: {
        type: Sequelize.STRING
      },
      createdAt: {
        allowNull: false,
        type: Sequelize.DATE
      },
      updatedAt: {
        allowNull: false,
        type: Sequelize.DATE
      }
    });
  },
  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('Recommendations');
  }
};