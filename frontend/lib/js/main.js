// @flow

import 'babel-polyfill';

import './fixEdge';

import React                      from 'react';
import ReactDOM                   from 'react-dom';
import {
  compose,
  applyMiddleware,
  createStore,
}                                 from 'redux';
import { Provider }               from 'react-redux';
import createHistory              from 'history/createBrowserHistory';

import './localization'; // To initialize locales
import './app/actions'; //  To initialize parameterized actions

import { BASENAME, isProduction } from './environment';
import createMiddleware           from './middleware';

import AppRouter                  from './app/AppRouter';
import reducers                   from './app/reducers';


require('es6-promise').polyfill();

require('font-awesome-webpack');
require('../styles/styles.sass');
require('../images/favicon.png');

// TODO: OG image required?
// require('../../images/og.png');
// Required for isomophic-fetch


function makeStore(initialState: Object, middleware) {
  let enhancer;

  if (!isProduction)
    enhancer = compose(
      middleware,
      // Use the Redux devtools extention, but only in development
      window.devToolsExtension ? window.devToolsExtension() : f => f,
    );
  else
    enhancer = compose(middleware);

  return createStore(reducers, initialState, enhancer);
}

const initialState = {};

// Redux Router setup
const browserHistory = createHistory({
  basename: BASENAME
});

const middleware = applyMiddleware(...createMiddleware(browserHistory));

const store = makeStore(initialState, middleware);

// ---------------------
// RENDER
// ---------------------
ReactDOM.render(
  <Provider store={store}>
    <AppRouter history={browserHistory} />
  </Provider>,
  document.getElementById('root')
);
