// @flow

// We need to put localStorage into an own constant to type annotate it.
const localStorage: Storage = window.localStorage;

export const getStoredAuthToken = () : ?string => {
  return localStorage.getItem('authToken');
};

export const storeAuthToken = (token: string) : void => {
  localStorage.setItem('authToken', token);
};

export const deleteStoredAuthToken = () : void => {
  localStorage.removeItem('authToken');
};
