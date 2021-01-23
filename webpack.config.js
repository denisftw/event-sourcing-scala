const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");

module.exports = {
  entry: './ui/entry.js',
  output: { 
    publicPath: '',
    path: path.resolve(__dirname, 'public/compiled'), 
    filename: 'bundle.js' 
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        include: /ui/,
        use: {
          loader: 'babel-loader'
        }
      },
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader',
          'sass-loader'
        ]
      },
      { 
        test: /\.(eot|woff|woff2|ttf|svg|png|jpg)$/, 
        use: {
          loader: 'url-loader?limit=1'
        }
      }
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: "styles.css" })
  ],
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin()],
  },
  devtool: process.env.NODE_ENV === 'development' ? 'source-map' : false
};