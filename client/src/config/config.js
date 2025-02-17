import _configValueSwitch from './config.value';

let NOW_ENV = 'LOCAL';
if (process.env.REACT_APP_ENV) {
  // Dockerfile build 的時候替換環境
  NOW_ENV = process.env.REACT_APP_ENV;
}
export const keys = {
  SERVER_POINT: 'SERVER_POINT',
  API_BASE_URL: 'API_BASE_URL'
};
const _valueSwitch = (value) => {
  return _configValueSwitch(value, NOW_ENV);
};

const config = {};
Object.keys(keys).forEach((key) => {
  config[key] = _valueSwitch(key);
});
config.API_BASE_URL = process.env.REACT_APP_API_BASE_URL || config.API_BASE_URL || "http://localhost:8080";

export default config;



