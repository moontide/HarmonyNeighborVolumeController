const path              = require('path'),
      precss            = require('precss'),
      webpack           = require('webpack'),
      autoprefixer      = require('autoprefixer'),
      ExtractTextPlugin = require("extract-text-webpack-plugin");

const config = {
  entry: {
    'js/app': './src/script/volume-controller',
    'css/style': './src/style/style',
  },
  output: {
    path: path.join(__dirname, 'dist'),
    filename: '[name].js',
  },
  resolve: {
    extensions: [ '.js', '.scss'],
  },
  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
        options: {
          presets: ['es2015', 'stage-0'],
          plugins: [
            'transform-decorators-legacy',
            'transform-object-rest-spread'
          ]
        }        
      },
      {
        test: /\.scss$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            'css-loader',
            {
              loader: 'postcss-loader',
              options: {
                plugins: () => [
                  precss,
                  autoprefixer
                ]
              }
            },
            'sass-loader'
          ],
        }),
      },
    ],
  },
  plugins: [
    new ExtractTextPlugin('css/style.css'),
  ],
};

if (process.env.NODE_ENV === 'production') {
  config.plugins.push(
    new webpack.optimize.UglifyJsPlugin({
      compress: { warnings: false }
    })
  );
} else {
  config.devtool = 'inline-source-map';
}

module.exports = config;