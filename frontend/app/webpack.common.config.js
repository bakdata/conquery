const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  plugins: [
    new HtmlWebpackPlugin({
      template: path.join(__dirname, 'src/index.tpl.html'),
      inject: 'body',
      filename: 'index.html'
    }),
    new webpack.NoEmitOnErrorsPlugin(),
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
  ],
  module: {
    rules: [{
      test: /\.js$/,
      exclude: path.join(__dirname, '../node_modules/'),
      loader: 'babel-loader'
    }, {
      test: /\.json$/,
      loader: 'json-loader'
    }, {
      test: /\.yml$/,
      loader: 'json-loader!yaml-loader'
    }, {
      test: /\.(ttf|eot|svg|png|jpg|woff(2)?)(\?.*$|$)/,
      loader: 'file-loader?name=[name].[ext]'
    }]
  }
};
