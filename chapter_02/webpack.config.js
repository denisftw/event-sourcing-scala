var webpack = require('webpack');
var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
  entry: './ui/entry.js',
  output: { path: __dirname + '/public/compiled', filename: 'bundle.js' },
  module: {
    loaders: [
      { test: /\.jsx?$/, loader: 'babel-loader', include: /ui/, query: { presets: ['es2015', 'stage-0', 'react'] } },
      { test: /\.scss$/, loader: ExtractTextPlugin.extract( "style", "css!sass") },
      { test: /\.(eot|woff|woff2|ttf|svg|png|jpg)$/, loader: 'url-loader?limit=1' }
    ]
  },
  plugins: [
    new ExtractTextPlugin("styles.css"),
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery',
      'window.jQuery': 'jquery'
    })
  ],
  devtool: 'source-map'
}
