{-# LANGUAGE OverloadedStrings #-}

import Data.Text -- Using Text to test the integer-simple flag handling with stack
import Lib1
import Lib2

main :: IO ()
main = do
  printGreeting $ hello $ unpack "world"
