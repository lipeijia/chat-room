import { keys } from './config';

const LOCAL = 'LOCAL';
const DEV = 'DEV';
const STAGE = 'STAGE';
const PROD = 'PROD';

let IP = '127.0.0.1';

export const configValue = (_target, _env) => {
  let result;
  switch (_target) {
    case keys.SERVER_POINT:
      if (_env === LOCAL) result = `http://${IP}:8080/`;
      if (_env === DEV) result = `https://sample.com/`;
      if (_env === STAGE) result = `https://sample/`;
      if (_env === PROD) result = `https://sample/`;
      break;
    default:
  }
  return result;
};

export default configValue;
