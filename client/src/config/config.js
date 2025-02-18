import _configValueSwitch from './config.value';

let NOW_ENV = 'LOCAL';
console.log(process.env.REACT_APP_ENV);
if (process.env.REACT_APP_ENV) {
  // Dockerfile build 的時候替換環境
  NOW_ENV = process.env.REACT_APP_ENV;
}
export const keys = {
  SERVER_POINT: 'SERVER_POINT',
  API_BASE_PORT: 'API_BASE_PORT'
};
const _valueSwitch = (value) => {
  return _configValueSwitch(value, NOW_ENV);
};

const config = {};
Object.keys(keys).forEach((key) => {
  config[key] = _valueSwitch(key);
});

export default config;



