using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using TMPro;
using UnityEngine.Networking;
using System;

public class VideoPause : MonoBehaviour
//, IPointerClickHandler, IPointerEnterHandler
{
    // Start is called before the first frame update
    public UnityEngine.Video.VideoPlayer videoPlayer;
    public UnityEngine.Video.VideoPlayer ClamVideoPlayer;
    public Button playPauseButton;
    // public Button Button2;
    public Button pauseButton;
    public Button ignoreButton;
    public GameObject panel;
    public GameObject notifPanel;
    public Button backButton;
    private string serverUrl = "https://bonefish-boss-singularly.ngrok-free.app/vr";
    public TextMeshProUGUI hrTextBox;
    private bool fl;
    public struct HeartRateData
    {
        public string hr;
        public string notifFlag;
    }

    void Start()
    {
        if (playPauseButton != null)
        {
            playPauseButton.onClick.AddListener(OnPlayPauseButtonClick);
            ignoreButton.onClick.AddListener(OnIgnoreButtonClick);
            pauseButton.onClick.AddListener(OnPauseButtonClick);
            backButton.onClick.AddListener(OnBackButtonClick);
            StartCoroutine(RepeatingRequest());
            fl = false;
        }
    }

    public void PlayVideo() {
        if (videoPlayer.isPaused) {
            videoPlayer.Play();
        }
    }

    public void PauseVideo() {
        if (videoPlayer.isPlaying) {
            videoPlayer.Pause();
        }
        else{
            videoPlayer.Play();
        }
    }



    void OnPlayPauseButtonClick()
    {
        Debug.Log("Button Clicked");
        PauseVideo();
        // if (panel != null)
        // {
        //     panel.SetActive(!panel.activeSelf); // This toggles the panel's visibility
        // }

        // if (notifPanel != null)
        // {
        //     notifPanel.SetActive(!notifPanel.activeSelf); // This toggles the panel's visibility
        // }

        // Add your logic here for what happens when the button is clicked
    }

    void OnIgnoreButtonClick()
    {
        PlayVideo();
        if (panel != null)
        {
            panel.SetActive(!panel.activeSelf); // This toggles the panel's visibility
        }

        if (notifPanel != null)
        {
            notifPanel.SetActive(!notifPanel.activeSelf); // This toggles the panel's visibility
        }
        fl = false;
    }

    void OnPauseButtonClick()
    {
        if (panel != null)
        {
            panel.SetActive(!panel.activeSelf); // This toggles the panel's visibility
        }

        if (notifPanel != null)
        {
            notifPanel.SetActive(!notifPanel.activeSelf); // This toggles the panel's visibility
        }
        ClamVideoPlayer.gameObject.SetActive(true);
        ClamVideoPlayer.Play();
        // playPauseButton.color.a=0;
        playPauseButton.gameObject.SetActive(false);
        backButton.gameObject.SetActive(true);
    }

    void OnBackButtonClick()
    {
        playPauseButton.gameObject.SetActive(true);
        backButton.gameObject.SetActive(false);
        ClamVideoPlayer.Pause();
        ClamVideoPlayer.gameObject.SetActive(false);
        videoPlayer.Play();
        fl = false;
    }

    IEnumerator RepeatingRequest()
    {
        while (true)
        {
            // Send the request
            yield return StartCoroutine(SendRequest());

            // Wait for 5 seconds
            yield return new WaitForSeconds(5);
        }
    }

    IEnumerator SendRequest()
    {
        using (UnityWebRequest webRequest = UnityWebRequest.Get(serverUrl))
        {
            
            // Send the request and wait for the response
            yield return webRequest.SendWebRequest();
            Debug.Log(webRequest.result);

            if (webRequest.result == UnityWebRequest.Result.ConnectionError || webRequest.result == UnityWebRequest.Result.ProtocolError)
            {
                Debug.LogError("Error: " + webRequest.error);
                PauseVideo();
            }
            else
            {
                
                Debug.Log("Response: " + webRequest.downloadHandler.text);

                try
                {
                    HeartRateData heartRateData = JsonUtility.FromJson<HeartRateData>(webRequest.downloadHandler.text);
                    hrTextBox.text = "Heart rate = " + heartRateData.hr;
                    bool flag = Convert.ToBoolean(heartRateData.notifFlag);
                    if (!fl)
                    {
                        if(flag) 
                        {
                            fl = true;
                            PauseVideo();
                            if (panel != null)
                            {
                                panel.SetActive(!panel.activeSelf); // This toggles the panel's visibility
                            }

                            if (notifPanel != null)
                            {
                                notifPanel.SetActive(!notifPanel.activeSelf); // This toggles the panel's visibility
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    Debug.LogError("JSON Parsing Error: " + e.Message);
                }

                // else
                // {
                //     canvas.SetActive(false);
            }
        }
        
    }


    //  public void OnPointerClick(PointerEventData eventData)
    // {
    //     // Handle click event
    //     Debug.Log("Button Clicked: " + gameObject.name);
    //     // Add your click handling logic here
    // }

    // public void OnPointerEnter(PointerEventData eventData)
    // {
    //     // Handle pointer enter event
    //     Debug.Log("Pointer Entered: " + gameObject.name);
    //     // Add your pointer enter handling logic here
    // }
}
// using UnityEngine;
// using UnityEngine.UI;

// public class VRButtonClickListener : MonoBehaviour
// {
//     public Button yourButton;

//     void Start()
//     {
//         if (yourButton != null)
//         {
//             yourButton.onClick.AddListener(OnButtonClick);
//         }
//     }

//     void OnButtonClick()
//     {
//         Debug.Log("Button Clicked");
//         // Add your logic here for what happens when the button is clicked
//     }
// }
