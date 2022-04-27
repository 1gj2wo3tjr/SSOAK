import { StyleSheet, Text } from "react-native";
import React from "react";
import Countdown, { zeroPad } from "react-countdown";
type Props = {};

const CountDown = ({ style }) => {
  return (
    <Countdown
      date={1650967623196 + 1000000000}
      intervalDelay={0}
      precision={0}
      // daysInHours={true}
      renderer={({ days, hours, minutes, seconds }) => (
        <Text style={style}>
          {Number(zeroPad(hours)) + Number(zeroPad(days)) * 24}시간{" "}
          {zeroPad(minutes)}분 {zeroPad(seconds)}초
        </Text>
      )}
    />
  );
};

export default CountDown;

const styles = StyleSheet.create({});
