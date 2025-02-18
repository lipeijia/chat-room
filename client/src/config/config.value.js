import { keys } from './config';

const LOCAL = 'LOCAL';
const DEV = 'DEV';
const STAGE = 'STAGE';
const PROD = 'PROD';
const DOCKER = 'DOCKER';
let IP = '127.0.0.1';
// IP = '192.168.1.101'; // peja

export const configValue = (_target, _env) => {
  let result;
  switch (_target) {
    case keys.SERVER_POINT:
      if (_env === LOCAL) result = `ws://${IP}:8080/`;
      if (_env === DEV) result = `wss://sample.com/`;
      if (_env === STAGE) result = `wss://sample/`;
      if (_env === PROD) result = `wss://sample/`;
      break;
    case keys.API_BASE_PORT:
      if (_env === LOCAL) result = `8080`;
      else if (_env === DOCKER) result = `8081`;
    default:
      break;
  }
  return result;
};

export default configValue;
