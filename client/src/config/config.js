import _configValueSwitch from './config.value';

let NOW_ENV = 'LOCAL';
if (process.env.REACT_APP_ENV)
  // dockerfile build 的時候替換環境
  NOW_ENV = process.env.REACT_APP_ENV;

export const keys = {
  SERVER_POINT: 'SERVER_POINT'
};
const _valueSwitch = (value) => {
  return _configValueSwitch(value, NOW_ENV);
};

const config = {};
Object.keys(keys).forEach((key) => {
  config[key] = _valueSwitch(key);
});

export default config;
