import { useState } from 'react'

function App() {

  const [selectedFile, setSelectedFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [result, setResult] = useState(null)

  const handleFileChange = (event) => {

    const file = event.target.files[0]

    if (file) {
      setSelectedFile(file)

      const imageUrl = URL.createObjectURL(file)

      setImagePreview(imageUrl)

      // Clear previous result
      setResult(null)
    }
  }

  const detectImage = async () => {

    if (!selectedFile) {
      alert("Please select an image first.")
      return
    }

    const formData = new FormData()

    formData.append("file", selectedFile)

    try {

      const response = await fetch(
          "http://localhost:8080/api/smogy/detect",
          {
            method: "POST",
            body: formData
          }
      )

      if (!response.ok) {
        throw new Error("Server error")
      }

      const data = await response.json()

      setResult(data)

    } catch (error) {

      console.error(error)

      alert("Failed to connect to the backend.")
    }
  }

  return (
      <div>

        <h1>AI Content Detector</h1>

        <p>
          Upload an image to detect whether it is AI-generated or human-made.
        </p>

        <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
        />

        {selectedFile && (
            <p>
              Selected file: {selectedFile.name}
            </p>
        )}

        {imagePreview && (
            <div>

              <h3>Image Preview</h3>

              <img
                  src={imagePreview}
                  alt="Selected"
                  width="300"
              />

            </div>
        )}

        <br />

        <button onClick={detectImage}>
          Detect Image
        </button>

        {result && (
            <div>

              <h2>Detection Result</h2>

              <p>
                Prediction: {result.prediction}
              </p>

              <p>
                Artificial: {result.artificialScore.toFixed(2)}%
              </p>

              <p>
                Human: {result.humanScore.toFixed(2)}%
              </p>

            </div>
        )}

      </div>
  )
}

export default App