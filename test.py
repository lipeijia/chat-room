#!/usr/bin/env python


import webbrowser    
from selenium import webdriver
from selenium.webdriver.common.by import By
import time
chrome_path="C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
urL1='http://localhost:89/example.html'
urL2='ws://localhost:89/example.html/ws?name='
edge = webdriver.Edge()

# webbrowser.register('edge', None,webbrowser.BackgroundBrowser(chrome_path))
for i in range(20):
    if i == 0:
        edge.get(urL1)
    else:
        
        edge.execute_script("window.open('example.html', '_blank');")
        edge.switch_to.window(edge.window_handles[i])
    edge.find_element(By.TAG_NAME, 'input').send_keys(str(i))
    edge.find_element(By.TAG_NAME, 'button').click()
  
       
time.sleep(99999)
    # edge.switch_to(i+1)


   

   
   
  
    # webbrowser.get('windows-default').open_new(urL)