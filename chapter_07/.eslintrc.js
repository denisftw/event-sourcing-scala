module.exports = {
  parser: '@babel/eslint-parser',
  plugins: [
    "react"
  ],
  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
  ],
  settings: {
    react: {
      version: 'detect'
    }
  },
  rules: {
    'react/prop-types': [2, { skipUndeclared: true }]
  },
  ignorePatterns: [
    '.eslintrc.js',
    'webpack.config.js'
  ],
  env: {
    browser: true,
    es2020: true,
  }
}